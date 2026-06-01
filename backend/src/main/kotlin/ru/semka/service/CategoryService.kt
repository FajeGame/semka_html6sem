package ru.semka.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.semka.domain.entity.CategoryEntity
import ru.semka.domain.enums.OperationType
import ru.semka.dto.CategoryDto
import ru.semka.dto.CreateCategoryRequest
import ru.semka.dto.UpdateCategoryRequest
import ru.semka.exception.ApiException
import ru.semka.repository.CategoryRepository
import ru.semka.repository.TransactionRepository
import ru.semka.security.AppUserDetails

/** категории доходов/расходов: CRUD только для владельца кошелька; удаление — если нет операций. */
@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val access: WalletAccessService,
) {

    // GET /categories/{id}
    fun get(categoryId: Long, user: AppUserDetails): CategoryDto {
        val cat = categoryRepository.findById(categoryId).orElseThrow { ApiException("NOT_FOUND", "категория не найдена") }
        access.requireMember(cat.walletId, user)
        return cat.toDto()
    }

    // GET /categories?walletId&tip
    fun list(walletId: Long, tip: OperationType?, user: AppUserDetails): List<CategoryDto> {
        access.requireMember(walletId, user)
        val list = if (tip != null) {
            categoryRepository.findByWalletIdAndTip(walletId, tip)
        } else {
            categoryRepository.findByWalletId(walletId)
        }
        return list.map { it.toDto() }
    }

    // POST /categories — только владелец
    @Transactional
    fun create(req: CreateCategoryRequest, user: AppUserDetails): CategoryDto {
        access.requireOwner(req.walletId, user)
        val cat = categoryRepository.save(
            CategoryEntity(
                walletId = req.walletId,
                name = req.name.trim(),
                tip = req.tip,
                iconKey = req.iconKey,
                colorBg = req.colorBg,
                createdBy = user.id,
            ),
        )
        return cat.toDto()
    }

    // PUT /categories/{id}
    @Transactional
    fun update(categoryId: Long, req: UpdateCategoryRequest, user: AppUserDetails): CategoryDto {
        val cat = categoryRepository.findById(categoryId).orElseThrow { ApiException("NOT_FOUND", "категория не найдена") }
        access.requireOwner(cat.walletId, user)
        cat.name = req.name.trim()
        cat.iconKey = req.iconKey
        cat.colorBg = req.colorBg
        return categoryRepository.save(cat).toDto()
    }

    // DELETE /categories/{id} — запрет, если есть операции
    @Transactional
    fun delete(categoryId: Long, user: AppUserDetails) {
        val cat = categoryRepository.findById(categoryId).orElseThrow { ApiException("NOT_FOUND", "категория не найдена") }
        access.requireOwner(cat.walletId, user)
        if (transactionRepository.countByCategoryId(categoryId) > 0) {
            throw ApiException("CONFLICT", "есть операции с этой категорией")
        }
        categoryRepository.delete(cat)
    }
}
